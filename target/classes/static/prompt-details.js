document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('jwtToken');
    const container = document.getElementById('prompt-details-container');
    const addVersionForm = document.getElementById('addVersionForm');
    const newVersionTextarea = document.getElementById('newVersionContent');

    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    const urlParams = new URLSearchParams(window.location.search);
    const promptId = urlParams.get('id');

    if (!promptId) {
        container.innerHTML = '<p style="color:red;">No prompt ID specified.</p>';
        return;
    }

    // --- Helper function for handling API responses ---
    function handleResponse(response) {
        if (response.status === 403) { // Token expired or invalid
            localStorage.removeItem('jwtToken');
            window.location.href = 'index.html';
            return Promise.reject(new Error('Session expired. Please log in again.'));
        }
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text || 'An API error occurred.') });
        }
        const contentType = response.headers.get("content-type");
        if (contentType && contentType.indexOf("application/json") !== -1) {
            return response.json();
        } else {
            return response.text();
        }
    }

    function fetchAndRenderPrompt() {
        fetch(`/prompts/${promptId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(handleResponse)
        .then(prompt => {
            let versionsHtml = (prompt.versions || [])
                .map(version => {
                    let feedbackHtml = (version.feedback || []).map(fb => `
                        <div class="feedback-item">
                            <strong>Rating: ${fb.rating}/5</strong>
                            <p>${escapeHtml(fb.comment)}</p>
                            <small>By: ${fb.user ? escapeHtml(fb.user.name) : 'Unknown'}</small>
                        </div>
                    `).join('');

                    return `
                        <div class="prompt-version" id="version-container-${version.id}">
                            <button class="edit-button" data-version-id="${version.id}">Edit</button>
                            <h4>Version ${version.versionNumber}</h4>
                            <div class="version-content">
                                <pre>${escapeHtml(version.content)}</pre>
                            </div>
                            <button class="enhance-button" data-version-id="${version.id}">Enhance with AI</button>
                            <div class="ai-suggestion-container"></div>
                            <div class="feedback-section">
                                <h5>Feedback</h5>
                                ${feedbackHtml.length > 0 ? feedbackHtml : '<p>No feedback yet.</p>'}
                                <form class="feedback-form" data-version-id="${version.id}">
                                    <input type="number" class="rating" min="1" max="5" placeholder="Rating (1-5)" required>
                                    <textarea class="comment" placeholder="Add a comment"></textarea>
                                    <button type="submit">Submit Feedback</button>
                                </form>
                            </div>
                        </div>
                    `;
                }).join('');

            container.innerHTML = `
                <h2>${escapeHtml(prompt.title)}</h2>
                <p>${escapeHtml(prompt.description)}</p>
                <hr>
                <h3>Versions</h3>
                ${versionsHtml}
            `;
        })
        .catch(error => {
            console.error('Error fetching details:', error);
            container.innerHTML = `<p style="color:red;">Error loading details: ${error.message}</p>`;
        });
    }

    function escapeHtml(text) {
        if (text === null || text === undefined) return '';
        const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
        return text.replace(/[&<>"']/g, function(m) { return map[m]; });
    }

    // This single event listener now handles all clicks
    document.body.addEventListener('click', function(event) {
        const target = event.target;

        // --- Edit Button Logic ---
        if (target && target.matches('.edit-button')) {
            const versionId = target.dataset.versionId;
            const versionContainer = document.querySelector(`#version-container-${versionId}`);
            const contentDiv = versionContainer.querySelector('.version-content');
            const originalContent = contentDiv.querySelector('pre').textContent;

            contentDiv.innerHTML = `
                <textarea class="edit-textarea" style="width: 95%; height: 150px; font-family: monospace;">${originalContent}</textarea>
                <div class="edit-controls">
                    <button class="save-edit-button" data-version-id="${versionId}">Save</button>
                    <button class="cancel-edit-button" type="button">Cancel</button>
                </div>
            `;
        }

        // --- Cancel Edit Button Logic ---
        if (target && target.matches('.cancel-edit-button')) {
            fetchAndRenderPrompt();
        }

        // --- Save Edit Button Logic ---
        if (target && target.matches('.save-edit-button')) {
            const button = target;
            const versionId = button.dataset.versionId;
            const versionContainer = document.querySelector(`#version-container-${versionId}`);
            const newContent = versionContainer.querySelector('.edit-textarea').value;

            if (!newContent.trim()) {
                alert('Version content cannot be empty.');
                return;
            }
            
            button.classList.add('loading');

            fetch(`/prompts/versions/${versionId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                body: JSON.stringify({ content: newContent })
            })
            .then(handleResponse)
            .then(() => fetchAndRenderPrompt())
            .catch(error => {
                console.error('Error saving changes:', error);
                alert(`Failed to save changes: ${error.message}`);
            })
            .finally(() => button.classList.remove('loading'));
        }

        // --- Enhance with AI Button Logic ---
        if (target && target.matches('.enhance-button')) {
            const button = target;
            const versionId = button.dataset.versionId;
            const suggestionContainer = document.querySelector(`#version-container-${versionId} .ai-suggestion-container`);
            
            button.classList.add('loading');
            suggestionContainer.innerHTML = '';

            fetch(`/api/ai/enhance/${versionId}`, {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}` }
            })
            .then(handleResponse)
            .then(suggestion => {
                suggestionContainer.innerHTML = `
                    <div class="ai-suggestion">
                        <pre class="suggestion-text">${escapeHtml(suggestion)}</pre>
                        <button class="apply-suggestion-button">Create New Version from this Suggestion</button>
                    </div>`;
            })
            .catch(error => {
                console.error('Error enhancing prompt:', error);
                suggestionContainer.innerHTML = `<p style="color:red;">Error getting AI suggestion: ${error.message}</p>`;
            })
            .finally(() => button.classList.remove('loading'));
        }

        // --- Apply AI Suggestion Button Logic ---
        if (target && target.matches('.apply-suggestion-button')) {
            const suggestionText = target.parentElement.querySelector('.suggestion-text').textContent;
            newVersionTextarea.value = suggestionText;
            addVersionForm.scrollIntoView({ behavior: 'smooth' });
            newVersionTextarea.focus();
        }
    });

    // --- Form Submission Logic (Feedback and Add Version) ---
    document.body.addEventListener('submit', function(event) {
        const form = event.target;
        
        if (form && form.matches('.feedback-form')) {
            event.preventDefault();
            const button = form.querySelector('button');
            button.classList.add('loading');
            const feedbackData = {
                rating: form.querySelector('.rating').value,
                comment: form.querySelector('.comment').value
            };
            fetch(`/feedback/${form.dataset.versionId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                body: JSON.stringify(feedbackData)
            })
            .then(handleResponse)
            .then(() => fetchAndRenderPrompt())
            .catch(error => console.error('Error submitting feedback:', error))
            .finally(() => button.classList.remove('loading'));
        }
        
        if (form && form.id === 'addVersionForm') {
            event.preventDefault();
            const button = addVersionForm.querySelector('button');
            button.classList.add('loading');
            const newContent = document.getElementById('newVersionContent').value;
            if (!newContent.trim()) {
                alert('New version content cannot be empty.');
                button.classList.remove('loading');
                return;
            }
            const versionData = { content: newContent };
            fetch(`/prompts/${promptId}/versions`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                body: JSON.stringify(versionData)
            })
            .then(handleResponse)
            .then(() => {
                document.getElementById('newVersionContent').value = '';
                fetchAndRenderPrompt();
            })
            .catch(error => console.error('Error adding version:', error))
            .finally(() => button.classList.remove('loading'));
        }
    });

    fetchAndRenderPrompt();
});
