import React from 'react';
import { Button } from 'react-bootstrap';

function Statistics() {
  return (
    <div>
      <div style={{ margin: '80px' }}>
        <h3 style={{ display: 'flex', justifyContent: 'center', marginBottom: '20px' }}>Liste des livres disponibles à l'emprunt :</h3>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Button style={{ marginLeft: '30px' }} > Globales </Button>
        <Button style={{ marginLeft: '30px' }} > Par livre </Button>
        <Button style={{ marginLeft: '30px' }} > Par utilisateur </Button>
      </div>

    </div>
  );
}
export default Statistics;