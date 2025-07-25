document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('jwtToken');
    const container = document.getElementById('prompt-details-container');
    const addVersionForm = document.getElementById('addVersionForm');

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

    function fetchAndRenderPrompt() {
        fetch(`/prompts/${promptId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(response => {
            if (!response.ok) throw new Error('Failed to fetch details');
            return response.json();
        })
        .then(prompt => {
            let versionsHtml = (prompt.versions || [])
                .map(version => {
                    let feedbackHtml = (version.feedback || []).map(fb => `
                        <div class="feedback-item">
                            <strong>Rating: ${fb.rating}/5</strong>
                            <p>${fb.comment}</p>
                            <small>By: ${fb.user ? fb.user.name : 'Unknown'}</small>
                        </div>
                    `).join('');

                    return `
                        <div class="prompt-version" id="version-container-${version.id}">
                            <h4>Version ${version.versionNumber}</h4>
                            <pre>${version.content}</pre>
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
                <h2>${prompt.title}</h2>
                <p>${prompt.description}</p>
                <hr>
                <h3>Versions</h3>
                ${versionsHtml}
            `;
            attachAllEventListeners();
        })
        .catch(error => console.error('Error fetching details:', error));
    }

    function attachAllEventListeners() {
        // --- Event Listener for Feedback Forms ---
        document.querySelectorAll('.feedback-form').forEach(form => {
            form.addEventListener('submit', function(event) {
                event.preventDefault();
                const button = form.querySelector('button');
                button.classList.add('loading');

                const versionId = form.dataset.versionId;
                const feedbackData = {
                    rating: form.querySelector('.rating').value,
                    comment: form.querySelector('.comment').value
                };
                fetch(`/feedback/${versionId}`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                    body: JSON.stringify(feedbackData)
                })
                .then(response => {
                    if (!response.ok) throw new Error('Failed to submit feedback');
                    fetchAndRenderPrompt();
                })
                .catch(error => {
                    console.error('Error submitting feedback:', error);
                    button.classList.remove('loading');
                });
            });
        });

        // --- Event Listener for AI Enhance Buttons ---
        document.querySelectorAll('.enhance-button').forEach(button => {
            button.addEventListener('click', function(event) {
                const versionId = event.target.dataset.versionId;
                const suggestionContainer = document.querySelector(`#version-container-${versionId} .ai-suggestion-container`);
                
                button.classList.add('loading');
                suggestionContainer.innerHTML = '';

                fetch(`/api/ai/enhance/${versionId}`, {
                    method: 'POST',
                    headers: { 'Authorization': `Bearer ${token}` }
                })
                .then(response => {
                    if (!response.ok) throw new Error('AI enhancement failed');
                    return response.text();
                })
                .then(suggestion => {
                    suggestionContainer.innerHTML = `<div class="ai-suggestion">${suggestion.replace(/\\n/g, '<br>')}</div>`;
                })
                .catch(error => {
                    console.error('Error enhancing prompt:', error);
                    suggestionContainer.innerHTML = '<p style="color:red;">Error getting AI suggestion.</p>';
                })
                .finally(() => {
                    button.classList.remove('loading');
                });
            });
        });
    }

    // --- Event Listener for Add New Version Form ---
    addVersionForm.addEventListener('submit', function(event) {
        event.preventDefault();
        const button = addVersionForm.querySelector('button');
        button.classList.add('loading');

        const versionData = { content: document.getElementById('newVersionContent').value };
        fetch(`/prompts/${promptId}/versions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify(versionData)
        })
        .then(response => {
            if (!response.ok) throw new Error('Failed to add version');
            document.getElementById('newVersionContent').value = '';
            fetchAndRenderPrompt();
        })
        .catch(error => console.error('Error adding version:', error))
        .finally(() => {
            button.classList.remove('loading');
        });
    });

    fetchAndRenderPrompt();
});
