document.addEventListener('DOMContentLoaded', function () {
    const user = JSON.parse(localStorage.getItem('user'));
    if (!user) {
        window.location.href = 'login.html';
        return;
    }

    const token = localStorage.getItem('token');
    const promptList = document.getElementById('prompt-list');
    const searchInput = document.getElementById('searchInput');
    const noResultsMessage = document.getElementById('no-results-message');

    function renderPrompts(prompts) {
        promptList.innerHTML = '';
        if (prompts.length === 0) {
            noResultsMessage.style.display = 'block';
        } else {
            noResultsMessage.style.display = 'none';
            prompts.forEach(prompt => {
                const div = document.createElement('div');
                div.classList.add('prompt-card');
                div.innerHTML = `
                    <h3>${prompt.title}</h3>
                    <p>${prompt.description}</p>
                    <pre>${prompt.versions[0]?.content || 'No content available'}</pre>
                    <div class="tags">
                        ${prompt.tags.map(tag => `<span class="tag">${tag.name}</span>`).join(' ')}
                    </div>
                `;
                promptList.appendChild(div);
            });
        }
    }

    function fetchPrompts() {
        fetch('/api/prompts', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(response => response.json())
        .then(data => {
            window.allPrompts = data; // Store all prompts for filtering
            renderPrompts(data);
        })
        .catch(error => {
            console.error('Error fetching prompts:', error);
        });
    }

    searchInput.addEventListener('input', function () {
        const searchTerm = this.value.toLowerCase();
        const filtered = window.allPrompts.filter(prompt =>
            prompt.title.toLowerCase().includes(searchTerm) ||
            prompt.description.toLowerCase().includes(searchTerm)
        );
        renderPrompts(filtered);
    });

    document.getElementById('logoutButton').addEventListener('click', function () {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        window.location.href = 'login.html';
    });

    document.getElementById('createPromptForm').addEventListener('submit', function (e) {
        e.preventDefault();

        const title = document.getElementById('promptTitle').value.trim();
        const description = document.getElementById('promptDescription').value.trim();
        const content = document.getElementById('promptContent').value.trim();
        const tagInput = document.getElementById('promptTags').value.trim();

        const tags = tagInput ? tagInput.split(',').map(tag => tag.trim()).filter(tag => tag) : [];

        const promptData = {
            title,
            description,
            prompt: content,
            tags
        };

        fetch('/api/prompts', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(promptData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to create prompt');
            }
            return response.json();
        })
        .then(data => {
            console.log('Prompt created:', data);
            fetchPrompts();
            document.getElementById('createPromptForm').reset();
        })
        .catch(error => {
            console.error('Error creating prompt:', error);
        });
    });

    fetchPrompts();
});
