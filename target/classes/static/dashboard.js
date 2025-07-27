document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('jwtToken');
    const promptListDiv = document.getElementById('prompt-list');
    const createPromptForm = document.getElementById('createPromptForm');
    const createPromptButton = createPromptForm.querySelector('button');
    const logoutButton = document.getElementById('logoutButton');

    if (!token) {
        window.location.href = 'index.html';
        return;
    }

    logoutButton.addEventListener('click', function() {
        localStorage.removeItem('jwtToken');
        window.location.href = 'index.html';
    });
    
    function fetchPrompts() {
        fetch('/prompts/my-prompts', {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        })
        .then(response => {
            if (!response.ok) {
                 // If token is invalid (e.g., expired), redirect to login
                if (response.status === 403) {
                    localStorage.removeItem('jwtToken');
                    window.location.href = 'index.html';
                }
                throw new Error('Could not fetch prompts');
            }
            return response.json();
        })
        .then(prompts => {
            promptListDiv.innerHTML = '';
            if (prompts.length === 0) {
                promptListDiv.innerHTML = '<p>You have no prompts yet. Create one below!</p>';
            } else {
                prompts.forEach(prompt => {
                    const promptElement = document.createElement('div');
                    promptElement.className = 'prompt-item';
                    
                    // Main content of the prompt, wrapped in a link
                    const linkElement = document.createElement('a');
                    linkElement.href = `prompt-details.html?id=${prompt.id}`;
                    linkElement.style.textDecoration = 'none'; 
                    linkElement.style.color = 'inherit';
                    linkElement.innerHTML = `
                        <h3>${prompt.title}</h3>
                        <p>${prompt.description}</p>
                        <small>Versions: ${prompt.versions.length}</small>
                    `;
                    
                    // Delete button
                    const deleteButton = document.createElement('button');
                    deleteButton.className = 'delete-button';
                    deleteButton.textContent = 'Delete';
                    deleteButton.dataset.promptId = prompt.id;

                    promptElement.appendChild(linkElement);
                    promptElement.appendChild(deleteButton);
                    promptListDiv.appendChild(promptElement);
                });
            }
        })
        .catch(error => {
            console.error('Error fetching prompts:', error);
            promptListDiv.innerHTML = '<p style="color:red;">Error loading prompts.</p>';
        });
    }

    // Event Delegation for Delete Buttons
    promptListDiv.addEventListener('click', function(event) {
        if (event.target && event.target.matches('.delete-button')) {
            const promptId = event.target.dataset.promptId;
            
            if (confirm('Are you sure you want to delete this prompt? This action cannot be undone.')) {
                fetch(`/prompts/${promptId}`, {
                    method: 'DELETE',
                    headers: { 'Authorization': `Bearer ${token}` }
                })
                .then(response => {
                    if (response.ok) {
                        fetchPrompts(); // Refresh the list on success
                    } else {
                        if (response.status === 403) {
                            alert('Error: You do not have permission to delete this prompt.');
                        } else {
                            alert('Failed to delete prompt.');
                        }
                    }
                })
                .catch(error => console.error('Error deleting prompt:', error));
            }
        }
    });

    createPromptForm.addEventListener('submit', function(event) {
        event.preventDefault();
        createPromptButton.classList.add('loading');

        const promptData = {
            title: document.getElementById('promptTitle').value,
            description: document.getElementById('promptDescription').value,
            content: document.getElementById('promptContent').value
        };

        fetch('/prompts', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(promptData)
        })
        .then(response => {
            if (!response.ok) throw new Error('Failed to create prompt');
            createPromptForm.reset();
            return response.json();
        })
        .then(() => {
            fetchPrompts();
        })
        .catch(error => console.error('Error creating prompt:', error))
        .finally(() => {
            createPromptButton.classList.remove('loading');
        });
    });

    fetchPrompts();
});
