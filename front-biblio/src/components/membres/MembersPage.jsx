//Import globaux
import { observer } from 'mobx-react-lite';
import React, { useEffect, useState, useContext } from 'react';
import RootStore from "../../RootStore";
// Import des popups
import MemberEditPopup from './Member_Edit';
import AddMemberPopup from './Member_Add';
import MemberInfoPopup from './Member_Infos';
import ConfirmationPopup from '../Confirmation_popup';
//import Bootstrap
import { Row, Col, Card, Button } from 'react-bootstrap';
import ListGroup from 'react-bootstrap/ListGroup';


function Members() {
  const { memberStore } = useContext(RootStore)
  const [selectedMember, setSelectedMember] = useState({ name: '', firstname: '', birthday: '' });

  // Gérer l'état des popups
  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isAddOpen, setIsAddOpen] = useState(false);
  const [isPlusOpen, setIsPlusOpen] = useState(false);

  // Charger les membres
  useEffect(() => {
    memberStore.fetchMembers();
  }, []);

  // Appel au ADD
  const handleAddCategory = (member) => {
    memberStore.addMember(member);
    setIsAddOpen(false);
  };

  const POPUP_EDIT = 'edit';
  const POPUP_ADD = 'add';
  const POPUP_PLUS = 'plus';

  function handlePopupClick(type, member) {
    setSelectedMember(member);
    switch (type) {
      case POPUP_EDIT:
        setIsEditOpen(true);
        break;
      case POPUP_ADD:
        setIsAddOpen(true);
        break;
      case POPUP_PLUS:
        setIsPlusOpen(true);
        break;
      default:
        break;
    }
  }

  function handlePopupClose() {
    setIsEditOpen(false);
    setIsAddOpen(false);
    setIsPlusOpen(false);
  }

  return (
    <div style={{ margin: '80px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '20px' }}>
        <h3 >Liste des utilisateurs</h3>
        <Button style={{ marginLeft: '30px' }} onClick={() => handlePopupClick("add", null)}>
          Ajouter un membre
        </Button>
      </div>
      <Row>
        {memberStore.members.map(member => (
          <Col xs={12} md={6} lg={4} className="mb-3" key={member.id}>
            <Card key={member.id}>
              <Card.Header style={{ textAlign: 'center' }}>
                <Card.Title> {member.name} </Card.Title>
              </Card.Header>
              <Card.Body >
                <ListGroup className="list-group-flush">
                  <ListGroup.Item>Nom : {member.name}</ListGroup.Item>
                  <ListGroup.Item>Prénom : {member.firstname} </ListGroup.Item>
                  <ListGroup.Item>Date de naissance : {member.birthday} </ListGroup.Item>
                </ListGroup>
              </Card.Body>
              <Card.Footer>
                <Button variant="link" onClick={() => handlePopupClick("plus", member)}> Infos emprunts </Button>
                <Button variant="link" onClick={() => handlePopupClick("edit", member)}> Editer </Button>
                <ConfirmationPopup title="Supprimer" message="Etes vous sur de vouloir supprimer cet utilisateur ? ?" onAction={() => memberStore.removeMember(member.id)} />
              </Card.Footer>
            </Card>
          </Col>
        ))}
      </Row>
      {isEditOpen &&
        <MemberEditPopup member={selectedMember} onClose={handlePopupClose} />
      }
      {isAddOpen &&
        <AddMemberPopup onClose={handlePopupClose} onSubmit={handleAddCategory} onCancel={handlePopupClose} />
      }
      {isPlusOpen &&
        <MemberInfoPopup memberId={selectedMember.id} onClose={handlePopupClose} />
      }
    </div>
  );
}
export default observer(Members);