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
                        <div class="prompt-version">
                            <h4>Version ${version.versionNumber}</h4>
                            <pre>${version.content}</pre>
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
            attachFeedbackFormListeners();
        })
        .catch(error => console.error('Error fetching details:', error));
    }

    function attachFeedbackFormListeners() {
        document.querySelectorAll('.feedback-form').forEach(form => {
            form.addEventListener('submit', function(event) {
                event.preventDefault();
                const button = form.querySelector('button');
                button.classList.add('loading'); // Start loading

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
                .catch(error => console.error('Error submitting feedback:', error))
                .finally(() => {
                    // This will run regardless of success or error
                    // We don't need to explicitly remove the class here because the page re-renders
                });
            });
        });
    }

    addVersionForm.addEventListener('submit', function(event) {
        event.preventDefault();
        const button = addVersionForm.querySelector('button');
        button.classList.add('loading'); // Start loading

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
            button.classList.remove('loading'); // Stop loading
        });
    });

    fetchAndRenderPrompt();
});