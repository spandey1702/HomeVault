document.addEventListener("DOMContentLoaded", function() {
    const uploadButton = document.getElementById("uploadDocumentButton");
    const viewDocumentButton = document.getElementById("viewDocumentButton");
    const viewItemButton = document.getElementById("viewItemButton");
    const notificationButton = document.getElementById("notificationButton");
    const expiringItemsList = document.getElementById("expiringItemsList");
    const logoutButton = document.getElementById("logoutButton");

    function checkSession() {
        fetch("/api/check-session")
            .then(response => {
                if (response.status === 401) {
                    window.location.href = "/login.html";
                }
            })
            .catch(error => console.error("Error checking session:", error));
    }

    function fetchExpiringItems() {
        fetch("/api/expiring-soon")
            .then(response => response.json())
            .then(data => {
                expiringItemsList.innerHTML = "";
                data.forEach(item => {
                    const listItem = document.createElement("li");
                    listItem.textContent = `Item: ${item.name}, Expiry Date: ${item.expiryDate}`;
                    expiringItemsList.appendChild(listItem);
                });
            })
            .catch(error => console.error("Error fetching expiring items:", error));
    }

    if (uploadButton) {
        uploadButton.addEventListener("click", function() {
            window.location.href = "/upload.html";
        });
    }

    if (viewDocumentButton) {
        viewDocumentButton.addEventListener("click", function() {
            window.location.href = "/view-docs.html";
        });
    }

    if (viewItemButton) {
        viewItemButton.addEventListener("click", function() {
            window.location.href = "/view-item.html";
        });
    }

    if (notificationButton) {
        notificationButton.addEventListener("click", function() {
            fetchExpiringItems();
            expiringItemsList.style.display = expiringItemsList.style.display === "none" ? "block" : "none";
        });
    }

    if (logoutButton) {
        logoutButton.addEventListener("click", function() {
            fetch("/api/logout", { method: "GET" })
                .then(response => {
                    if (response.ok) {
                        window.location.href = "/login.html";
                    }
                })
                .catch(error => console.error("Error logging out:", error));
        });
    }

    checkSession();
});
