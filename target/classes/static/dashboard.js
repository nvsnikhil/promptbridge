document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('jwtToken');
    const promptListDiv = document.getElementById('prompt-list');
    const createPromptForm = document.getElementById('createPromptForm');
    const createPromptButton = createPromptForm.querySelector('button');
    const logoutButton = document.getElementById('logoutButton');
    const searchInput = document.getElementById('searchInput');
    const noResultsMessage = document.getElementById('no-results-message');

    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    logoutButton.addEventListener('click', function() {
        localStorage.removeItem('jwtToken');
        window.location.href = 'index.html';
    });

    // --- Helper function for handling API responses ---
    function handleApiResponse(response) {
        if (response.status === 403) { // Token expired or invalid
            localStorage.removeItem('jwtToken');
            window.location.href = 'index.html';
            return Promise.reject(new Error('Session expired. Please log in again.'));
        }
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text || 'An API error occurred.') });
        }
        if (response.status === 204) {
            return Promise.resolve();
        }
        return response.json();
    }
    
    // --- Helper function to escape HTML ---
    function escapeHtml(text) {
        if (text === null || text === undefined) return '';
        const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
        return text.replace(/[&<>"']/g, function(m) { return map[m]; });
    }

    function renderPrompts(prompts) {
        promptListDiv.innerHTML = '';
        noResultsMessage.style.display = 'none';

        if (prompts.length === 0) {
            if (searchInput.value.trim() !== '') {
                noResultsMessage.style.display = 'block';
            } else {
                promptListDiv.innerHTML = '<p>You have no prompts yet. Create one below!</p>';
            }
        } else {
            prompts.forEach(prompt => {
                const promptElement = document.createElement('div');
                promptElement.className = 'prompt-item';
                
                // This is the new logic to display tags
                let tagsHtml = '';
                if (prompt.tags && prompt.tags.length > 0) {
                    tagsHtml = '<div class="tags-container">' + prompt.tags.map(tag => `<span class="tag">${escapeHtml(tag.name)}</span>`).join('') + '</div>';
                }

                const linkElement = document.createElement('a');
                linkElement.href = `prompt-details.html?id=${prompt.id}`;
                linkElement.style.textDecoration = 'none';
                linkElement.style.color = 'inherit';
                linkElement.innerHTML = `
                    <h3>${escapeHtml(prompt.title)}</h3>
                    <p>${escapeHtml(prompt.description)}</p>
                    ${tagsHtml}
                    <small>Versions: ${prompt.versions.length}</small>
                `;
                
                const deleteButton = document.createElement('button');
                deleteButton.className = 'delete-button';
                deleteButton.textContent = 'Delete';
                deleteButton.dataset.promptId = prompt.id;

                promptElement.appendChild(linkElement);
                promptElement.appendChild(deleteButton);
                promptListDiv.appendChild(promptElement);
            });
        }
    }
    
    function fetchPrompts(query = '') {
        let url = query ? `/prompts/search?query=${encodeURIComponent(query)}` : '/prompts/my-prompts';

        fetch(url, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(handleApiResponse)
        .then(renderPrompts)
        .catch(error => {
            console.error('Error fetching prompts:', error);
            promptListDiv.innerHTML = `<p style="color:red;">Error loading prompts: ${error.message}</p>`;
        });
    }
    
    let searchTimeout;
    searchInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            fetchPrompts(this.value);
        }, 300);
    });

    promptListDiv.addEventListener('click', function(event) {
        if (event.target && event.target.matches('.delete-button')) {
            const promptId = event.target.dataset.promptId;
            if (confirm('Are you sure you want to delete this prompt? This action cannot be undone.')) {
                fetch(`/prompts/${promptId}`, {
                    method: 'DELETE',
                    headers: { 'Authorization': `Bearer ${token}` }
                })
                .then(handleApiResponse)
                .then(() => {
                    fetchPrompts(searchInput.value);
                })
                .catch(error => {
                    console.error('Error deleting prompt:', error);
                    alert(`Failed to delete prompt: ${error.message}`);
                });
            }
        }
    });

    createPromptForm.addEventListener('submit', function(event) {
        event.preventDefault();
        createPromptButton.classList.add('loading');

        // This is the new logic to read the tags from the input field
        const tagsInput = document.getElementById('promptTags').value;
        const tags = tagsInput ? tagsInput.split(',').map(tag => tag.trim()).filter(tag => tag) : [];

        const promptData = {
            title: document.getElementById('promptTitle').value,
            description: document.getElementById('promptDescription').value,
            prompt: document.getElementById('promptContent').value,
            tags: tags // Add the tags to the payload
        };

        console.log("Sending Prompt Details:", promptData);



        fetch('/prompts', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
            body: JSON.stringify(promptData)
        })
        .then(handleApiResponse)
        .then(() => {
            createPromptForm.reset();
            searchInput.value = '';
            fetchPrompts();
        })
        .catch(error => {
            console.error('Error creating prompt:', error);
            alert(`Failed to create prompt: ${error.message}`);
        })
        .finally(() => {
            createPromptButton.classList.remove('loading');
        });
    });

    fetchPrompts();
});
