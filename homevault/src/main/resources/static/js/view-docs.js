document.addEventListener("DOMContentLoaded", function() {
    const documentsList = document.getElementById("documentsList");
    const backButton = document.getElementById("backButton");

    function fetchDocuments() {
        fetch("/api/documents/list")
            .then(response => response.json())
            .then(data => {
                documentsList.innerHTML = "";
                data.forEach(doc => {
                    const listItem = document.createElement("li");
                    listItem.innerHTML = `
                        <span>File: ${doc.filePath.split('/').pop()}</span>
                        <button data-id="${doc.id}" class="downloadButton">Download</button>
                    `;
                    documentsList.appendChild(listItem);
                });

                
                document.querySelectorAll(".downloadButton").forEach(button => {
                    button.addEventListener("click", function() {
                        const id = this.getAttribute("data-id");
                        window.location.href = `/api/documents/download/${id}`;
                    });
                });
            })
            .catch(error => console.error("Error fetching documents:", error));
    }

    if (backButton) {
        backButton.addEventListener("click", function() {
            window.location.href = "/home.html";
        });
    }

    fetchDocuments();
});
