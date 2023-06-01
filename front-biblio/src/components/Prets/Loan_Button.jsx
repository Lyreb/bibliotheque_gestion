// Composant EmpruntButton.js
import React from 'react';
import Button from 'react-bootstrap/Button';

function EmpruntButton({ bookId }) {
  return (
    <Button variant="link" key={book.id} onClick={() => history.push(`${url}/${bookId}`)}>Détails</Button>
  );
}
export default EmpruntButton;