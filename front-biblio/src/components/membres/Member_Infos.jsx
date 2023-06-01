//Import globaux
import React, { useEffect, useState, useContext } from 'react';
import { Modal, Button, Container, Row, Col, Card } from 'react-bootstrap';
import RootStore from '../../RootStore';
// Import composant
import ConfirmationPopup from '../Confirmation_popup';

function MemberInfoPopup(props) {
  const { memberStore, bookStore } = useContext(RootStore);
  const { memberId } = props;
  const [isLoaded, setIsLoaded] = useState(false);
  const [member, setMember] = useState([]);

  useEffect(() => {
    fetch(`http://127.0.0.1:8080/api/v1/rest/members/${memberId}`)
      .then(res => res.json())
      .then(
        (result) => {
          setIsLoaded(true);
          setMember(result);
        },
        (error) => {
          setIsLoaded(true);
          setError(error);
        }
      )
  }, [memberId])

  if (!member) {
    return <div>Loading...</div>;
  }

  return (
    <Modal show={true} onHide={props.onClose}>
      <Modal.Header closeButton>
        <Modal.Title>Informations et Emprunts de {member.firstname} {member.name}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Container>
          <Row>
            <Col>
              <h4>Informations générales</h4>
              <p>ID : {member.id}</p>
              <p>Nom : {member.name}</p>
              <p>Prénom : {member.firstname}</p>
              <p>Date de naissance : {member.birthday}</p>
            </Col>
          </Row>
          <Row>
            <Col>
              <h4>Emprunt(s) de livre(s)</h4>
              {member.loans && member.loans.length > 0 ? (
                <Row>
                  {member.loans.map((loan, index) => (
                    <Col key={index} style={{ marginBottom: "10px" }}>
                      <Card>
                        <Card.Header>
                          <Card.Title>ID de la copie : {loan.bookCopy.id}</Card.Title>
                        </Card.Header>
                        <Card.Body>
                          <Card.Text>Etat de la copie : {loan.bookCopy.state}</Card.Text>
                          {loan.returnState === null ?
                            <Card.Text>emprunt en cours depuis le {loan.loanDateTime}</Card.Text>
                            :
                            <>
                              <Card.Text>du {loan.loanDateTime} au {loan.returnDateTime}</Card.Text>
                              <Card.Text>Etat initial : {loan.initialState}</Card.Text>
                              <Card.Text>Etat retourné : {loan.returnState}</Card.Text>
                            </>
                          }
                        </Card.Body>
                        {loan.returnState === null ?
                          <Card.Footer>
                            <ConfirmationPopup title="Retourner le livre" message="Etes vous sur de vouloir retourner ce livre ?" onAction={() => memberStore.returnBook(loan.bookCopy.id)} />
                          </Card.Footer> :
                          <>
                          </>
                        }
                      </Card>
                    </Col>
                  ))}
                </Row>
              ) : (
                <p>Aucun emprunt passé ou en cours</p>
              )}
            </Col>
          </Row>
        </Container>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={props.onClose}>
          Fermer
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
export default MemberInfoPopup;