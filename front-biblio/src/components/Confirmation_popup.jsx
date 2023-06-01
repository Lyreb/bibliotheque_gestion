import React, { useState } from 'react';
import { Modal, Button } from 'react-bootstrap';

function ConfirmationPopup({ title, message, onAction }) {
  const [showModal, setShowModal] = useState(false);

  function handleModale() {
    setShowModal(!showModal);
  }

  return (
    <>
      <Button variant="link" onClick={() => handleModale()}>{title}</Button>
      <Modal onHide={handleModale} show={showModal}>
        <Modal.Header closeButton>
          <Modal.Title>{title}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <p>{message}</p>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="danger" type="submit" onClick={onAction}>Confirmer</Button>
          <Button variant="secondary" onClick={handleModale}>Annuler</Button>
        </Modal.Footer>
      </Modal>
    </>
  );
}
export default ConfirmationPopup;