import { observer } from "mobx-react-lite";
import React, { useContext, useEffect, useState } from "react";
import RootStore from "../../RootStore";
import { Modal, Form, Button } from 'react-bootstrap';

function EmprunterLivre(onClose,) {
    const { memberStore } = useContext(RootStore)
    const [members, setMembers] = useState([]);

    // Charger les membres
    useEffect(() => {
        memberStore.fetchMembers();
    }, []);

    // SELECTION DU MEMEBRE 
    const handleMemberChange = (event) => {
        const selectedMember = Array.from(
            event.target.selectedOptions,
            (option) => option.value
        );
        setMembers(selectedMember);
    };

    return (
        <Modal show={true} onHide={onClose}>
            <Form>
                <Modal.Header closeButton>
                    <Modal.Title>Choisir un membre</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Form.Group>
                        <Form.Label>Membre :</Form.Label>
                        <Form.Select
                            multiple
                            value={members}
                            onChange={handleMemberChange}
                        >
                            {memberStore.members.map((membre) => (
                                <option key={membre.id} value={membre.name}>
                                    {membre.name}
                                </option>
                            ))}
                        </Form.Select>
                    </Form.Group>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="primary" type="submit">
                        Emprunter
                    </Button>
                    <Button variant="secondary" onClick={onClose}>
                        Annuler
                    </Button>
                </Modal.Footer>
            </Form>
        </Modal>
    );
}
export default observer(EmprunterLivre);