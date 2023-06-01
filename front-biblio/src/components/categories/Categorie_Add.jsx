import React, { useState } from "react";
import { Modal, Button, Form } from "react-bootstrap";

function AddCategoryPopup(props) {
  const [code, setCode] = useState("");
  const [name, setName] = useState("");
  const [adultOnly, setAdultOnly] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    props.onSubmit({ code, name, adultOnly });
    setCode("");
    setName("");
    setAdultOnly(false);
  };

  return (
    <Modal show={true} onHide={props.onCancel}>
      <Modal.Header closeButton>
        <Modal.Title>Ajout category</Modal.Title>
      </Modal.Header>
      <Form onSubmit={handleSubmit}>
        <Modal.Body>
          <Form.Group>
            <Form.Label>Code:</Form.Label>
            <Form.Control
              type="text"
              value={code}
              onChange={(e) => setCode(e.target.value)}
            />
          </Form.Group>
          <Form.Group>
            <Form.Label>Name:</Form.Label>
            <Form.Control
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </Form.Group>
          <Form.Group>
            <Form.Check
              type="checkbox"
              label="Adult Only"
              checked={adultOnly}
              onChange={(e) => setAdultOnly(e.target.checked)}
            />
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button type="submit">Add Category</Button>
          <Button variant="secondary" onClick={props.onCancel}>
            Cancel
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
}
export default AddCategoryPopup;