//Import globaux
import { observer } from 'mobx-react-lite';
import React, { useState, useContext } from 'react';
import RootStore from "../../RootStore";
//import Bootstrap
import { Modal, Button, Form } from 'react-bootstrap';

function MemberEditPopup({ member, onClose }) {
  const { memberStore } = useContext(RootStore);
  const [name, setName] = useState(member.name);
  const [firstname, setFirstname] = useState(member.firstname);
  const [birthday, setBirthday] = useState(member.birthday);

  function handleSubmit(event) {
    event.preventDefault();
    memberStore.editMember(member.id, { name, firstname, birthday }).then(onClose);
  }

  return (
    <Modal show={true} onHide={onClose}>
      <Form onSubmit={handleSubmit}>
        <Modal.Header closeButton>
          <Modal.Title>Membre {member.name}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form.Group controlId="formId">
            <Form.Label>ID:</Form.Label>
            <Form.Control type="text" name="id" value={member.id} disabled />
          </Form.Group>
          <Form.Group controlId="formName">
            <Form.Label>Nom:</Form.Label>
            <Form.Control type="text" name="name" value={name} onChange={(e) => setName(e.target.value)} />
          </Form.Group>
          <Form.Group controlId="formFirstname">
            <Form.Label>Prénom:</Form.Label>
            <Form.Control type="text" name="firstname" value={firstname} onChange={(e) => setFirstname(e.target.value)} />
          </Form.Group>
          <Form.Group controlId="formBirthday">
            <Form.Label>Date de naissance:</Form.Label>
            <Form.Control type="date" name="birthday" value={birthday} onChange={(e) => setBirthday(e.target.value)} />
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={onClose}>Annuler</Button>
          <Button variant="primary" type="submit">Enregistrer</Button>
        </Modal.Footer>
      </Form>
    </Modal>

  );
}
export default observer(MemberEditPopup);