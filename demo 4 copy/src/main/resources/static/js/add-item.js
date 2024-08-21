document.getElementById('addItemForm').addEventListener('submit', function(e) {
    e.preventDefault();
    fetch('/api/items/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
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
        window.location.href = '/items';
    })
    .catch(error => {
        alert('Error: ' + error.message);
    });
});
