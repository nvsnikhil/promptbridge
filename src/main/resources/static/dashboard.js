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
        .then(response => response.json())
        .then(prompts => {
            promptListDiv.innerHTML = '';
            if (prompts.length === 0) {
                promptListDiv.innerHTML = '<p>You have no prompts yet. Create one below!</p>';
            } else {
                prompts.forEach(prompt => {
                    const promptElement = document.createElement('a');
                    promptElement.href = `prompt-details.html?id=${prompt.id}`;
                    promptElement.className = 'prompt-item';
                    promptElement.innerHTML = `
                        <h3>${prompt.title}</h3>
                        <p>${prompt.description}</p>
                        <small>Versions: ${prompt.versions.length}</small>
                    `;
                    promptListDiv.appendChild(promptElement);
                });
            }
        })
        .catch(error => {
            console.error('Error fetching prompts:', error);
            promptListDiv.innerHTML = '<p style="color:red;">Error loading prompts.</p>';
        });
    }

    createPromptForm.addEventListener('submit', function(event) {
        event.preventDefault();
        createPromptButton.classList.add('loading'); // Start loading

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
            fetchPrompts();
        })
        .catch(error => console.error('Error creating prompt:', error))
        .finally(() => {
            createPromptButton.classList.remove('loading'); // Stop loading
        });
    });

    fetchPrompts();
});