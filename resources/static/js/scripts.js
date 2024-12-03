// JavaScript function to handle theme toggling
function toggleTheme() {
    document.body.classList.toggle('dark-theme');

    if (document.body.classList.contains('dark-theme')) {
	localStorage.setItem('theme', 'dark');
    } else {
	localStorage.setItem('theme', 'light');
    }
    // alert("Theme changed!")
}

// Check and apply the saved theme preference on page load
window.onload = function() {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
	document.body.classList.add('dark-theme');
    }
};

// Sidebar
const toggleButton = document.getElementById('toggle-button');
const sidebar = document.getElementById('sidebar');

toggleButton.addEventListener('click', function(e) {
    if (sidebar.classList.contains('collapsed')) {
        sidebar.classList.remove('collapsed');
        sidebar.classList.add('expanded');
	e.target.textContent = ">";
    } else {
        sidebar.classList.remove('expanded');
        sidebar.classList.add('collapsed');
	e.target.textContent = "<";
    }
});
