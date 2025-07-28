document.addEventListener("DOMContentLoaded", () => {
  const userNameSpan = document.getElementById("userName");
  const logoutButton = document.getElementById("logoutButton");
  const darkToggle = document.getElementById("darkModeToggle");

  // Set user's name
  const storedUser = JSON.parse(localStorage.getItem("user"));
  if (storedUser?.displayName) {
    userNameSpan.textContent = storedUser.displayName;
  }

  // Logout
  logoutButton.addEventListener("click", () => {
    localStorage.removeItem("user");
    window.location.href = "index.html";
  });

  // Dark mode toggle
  darkToggle.addEventListener("change", () => {
    document.body.classList.toggle("bg-white");
    document.body.classList.toggle("bg-gray-900");
    document.body.classList.toggle("text-gray-900");
    document.body.classList.toggle("text-white");
  });

  // Elements
  const createPromptForm = document.getElementById("createPromptForm");
  const promptList = document.getElementById("prompt-list");
  const searchInput = document.getElementById("searchInput");
  const noResultsMessage = document.getElementById("no-results-message");

  // Prompt data store
  let prompts = JSON.parse(localStorage.getItem("prompts")) || [];

  // Render prompts
  const renderPrompts = (data) => {
    promptList.innerHTML = "";
    if (data.length === 0) {
      noResultsMessage.classList.remove("hidden");
      return;
    } else {
      noResultsMessage.classList.add("hidden");
    }

    data.forEach((prompt, index) => {
      const promptCard = document.createElement("div");
      promptCard.className = "bg-white border border-gray-300 rounded p-4 shadow";

      const title = document.createElement("h3");
      title.className = "text-lg font-semibold text-blue-600 mb-2";
      title.textContent = prompt.title;

      const description = document.createElement("p");
      description.className = "text-sm text-gray-700 mb-2";
      description.textContent = prompt.description;

      const tags = document.createElement("p");
      tags.className = "text-sm text-gray-500 italic mb-2";
      tags.textContent = prompt.tags;

      const content = document.createElement("pre");
      content.className = "bg-gray-100 p-2 rounded overflow-x-auto text-sm";
      content.textContent = prompt.content;

      promptCard.appendChild(title);
      promptCard.appendChild(description);
      promptCard.appendChild(tags);
      promptCard.appendChild(content);

      promptList.appendChild(promptCard);
    });
  };

  // Initial render
  renderPrompts(prompts);

  // Search
  searchInput.addEventListener("input", (e) => {
    const keyword = e.target.value.toLowerCase();
    const filtered = prompts.filter(
      (p) =>
        p.title.toLowerCase().includes(keyword) ||
        p.description.toLowerCase().includes(keyword)
    );
    renderPrompts(filtered);
  });

  // Create new prompt
  createPromptForm.addEventListener("submit", (e) => {
    e.preventDefault();

    const newPrompt = {
      title: document.getElementById("promptTitle").value,
      description: document.getElementById("promptDescription").value,
      tags: document.getElementById("promptTags").value,
      content: document.getElementById("promptContent").value,
    };

    prompts.unshift(newPrompt); // add to top
    localStorage.setItem("prompts", JSON.stringify(prompts));
    renderPrompts(prompts);
    createPromptForm.reset();
  });
});
