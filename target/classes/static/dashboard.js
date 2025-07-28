document.addEventListener("DOMContentLoaded", function () {
  const logoutButton = document.getElementById("logoutButton");
  const promptList = document.getElementById("prompt-list");
  const createPromptForm = document.getElementById("createPromptForm");
  const searchInput = document.getElementById("searchInput");
  const noResultsMessage = document.getElementById("no-results-message");
  const themeToggle = document.getElementById("themeToggle");
  const usernameDisplay = document.getElementById("usernameDisplay");

  themeToggle.addEventListener("change", () => {
    document.documentElement.classList.toggle("dark");
  });

  const username = localStorage.getItem("username") || "User";
  usernameDisplay.textContent = `Welcome, ${username}`;

  let prompts = JSON.parse(localStorage.getItem("prompts")) || [];

  function renderPrompts(filteredPrompts = prompts) {
    promptList.innerHTML = "";
    if (filteredPrompts.length === 0) {
      noResultsMessage.classList.remove("hidden");
      return;
    }
    noResultsMessage.classList.add("hidden");

    filteredPrompts.forEach((prompt, index) => {
      const card = document.createElement("div");
      card.className = "bg-white dark:bg-gray-800 p-4 rounded shadow";
      card.innerHTML = `
        <h3 class="text-xl font-semibold mb-1">${prompt.title}</h3>
        <p class="mb-2">${prompt.description}</p>
        <div class="flex flex-wrap gap-2 mb-2">
          ${prompt.tags
            .map(tag => `<span class="bg-blue-100 dark:bg-blue-800 text-blue-800 dark:text-blue-100 px-2 py-1 text-xs rounded">${tag.trim()}</span>`)
            .join("")}
        </div>
        <pre class="bg-gray-100 dark:bg-gray-900 p-2 rounded overflow-x-auto"><code>${prompt.content}</code></pre>
      `;
      promptList.appendChild(card);
    });
  }

  renderPrompts();

  createPromptForm.addEventListener("submit", function (e) {
    e.preventDefault();

    const title = document.getElementById("promptTitle").value;
    const description = document.getElementById("promptDescription").value;
    const tags = document.getElementById("promptTags").value.split(",");
    const content = document.getElementById("promptContent").value;

    const newPrompt = { title, description, tags, content };
    prompts.push(newPrompt);
    localStorage.setItem("prompts", JSON.stringify(prompts));
    renderPrompts();
    createPromptForm.reset();
  });

  searchInput.addEventListener("input", function () {
    const searchTerm = this.value.toLowerCase();
    const filtered = prompts.filter(prompt =>
      prompt.title.toLowerCase().includes(searchTerm) ||
      prompt.description.toLowerCase().includes(searchTerm)
    );
    renderPrompts(filtered);
  });

  logoutButton.addEventListener("click", function () {
    localStorage.removeItem("username");
    window.location.href = "login.html";
  });
});
