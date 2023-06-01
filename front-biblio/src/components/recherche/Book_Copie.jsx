import { observer } from "mobx-react-lite";
import React from "react";
import { Card } from "react-bootstrap";

function BookCopy(props) {
  return (
    <div>
      <Card key={book.id}>
        <Card.Header style={{ textAlign: 'center' }}>
          <Card.Title>Identifiant : {props.book.id}</Card.Title>
        </Card.Header>
        <p>Etat : {props.book.title}</p>
      </Card>

    </div>
  );
}
export default observer(BookCopy);