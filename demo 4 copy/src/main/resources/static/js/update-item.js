function getQueryParam(name) {
    const params = new URLSearchParams(window.location.search);
    return params.get(name);
}

document.addEventListener('DOMContentLoaded', function() {
    const itemId = getQueryParam('id');
    if (itemId) {
        fetch(`/api/items/${itemId}`)
            .then(response => response.json())
            .then(item => {
                document.getElementById('itemId').value = item.id;
                document.getElementById('name').value = item.name;
                document.getElementById('category').value = item.category;
                document.getElementById('description').value = item.description;
                document.getElementById('purchaseDate').value = item.purchaseDate;
                document.getElementById('value').value = item.value;
                document.getElementById('expiryDate').value = item.expiryDate;
            })
            .catch(error => {
                alert('Error: ' + error.message);
            });
    }
});

document.getElementById('updateItemForm').addEventListener('submit', function(e) {
    e.preventDefault();
    fetch('/api/items/update', {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            id: document.getElementById('itemId').value,
            name: document.getElementById('name').value,
            category: document.getElementById('category').value,
            description: document.getElementById('description').value,
            purchaseDate: document.getElementById('purchaseDate').value,
            value: parseFloat(document.getElementById('value').value),
            expiryDate: document.getElementById('expiryDate').value
        })
    })
    .then(response => response.text())
    .then(message => {
        alert(message);
        window.location.href = '/items/view';
    })
    .catch(error => {
        alert('Error: ' + error.message);
    });
});
