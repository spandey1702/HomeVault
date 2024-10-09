function fetchItems() {
    fetch('/api/items')
        .then(response => response.json())
        .then(items => {
            const tbody = document.querySelector('#itemsTable tbody');
            tbody.innerHTML = '';
            items.forEach(item => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${item.id}</td>
                    <td>${item.name}</td>
                    <td>${item.category}</td>
                    <td>${item.description}</td>
                    <td>${item.purchaseDate}</td>
                    <td>${item.value}</td>
                    <td>${item.expiryDate}</td>
                    <td>
                        <button onclick="updateItem(${item.id})">Update</button>
                        <button onclick="deleteItem(${item.id})">Delete</button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        })
        .catch(error => {
            alert('Error: ' + error.message);
        });
}

function deleteItem(id) {
    fetch(`/api/delete/${id}`, { method: 'DELETE' })
        .then(response => response.text())
        .then(message => {
            alert(message);
            fetchItems();
        })
        .catch(error => {
            alert('Error: ' + error.message);
        });
}

function updateItem(id) {
    window.location.href = `/update?id=${id}`;
}

document.addEventListener('DOMContentLoaded', fetchItems);
