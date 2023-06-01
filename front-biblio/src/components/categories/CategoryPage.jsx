//Import globaux
import { observer } from "mobx-react";
import React, { useState, useEffect, useContext } from "react";
import RootStore from "../../RootStore";
// Import des composants
import AddCategoryPopup from "./Categorie_Add";
import ConfirmationPopup from "../Confirmation_popup";
import CategorieEdit from './Categorie_Edit';
//import Bootstrap
import Card from 'react-bootstrap/Card';
import ListGroup from 'react-bootstrap/ListGroup';
import CardGroup from 'react-bootstrap/CardGroup';
import Button from 'react-bootstrap/Button';
import { Form, Row, Col } from 'react-bootstrap';


function Categories() {
    const { categoryStore } = useContext(RootStore)
    const [selectedCategory, setSelectedCategory] = useState({ name: '', code: '', adultOnly: '' });

    // Gérer l'état des popups
    const [isEditOpen, setIsEditOpen] = useState(false);
    const [isAddOpen, setIsAddOpen] = useState(false);
    const [isDeleteOpen, setIsDeleteOpen] = useState(false);

    // Charger les catégories
    useEffect(() => {
        categoryStore.fetchCategories();
    }, []);

    // Appel au ADD
    const handleAddCategory = (categorie) => {
        categoryStore.addCategory(categorie);
        setIsAddOpen(false);
    };

    const POPUP_EDIT = 'edit';
    const POPUP_DELETE = 'delete';
    const POPUP_ADD = 'add';

    function handlePopupClick(type, categorie) {
        setSelectedCategory(categorie);
        switch (type) {
            case POPUP_EDIT:
                setIsEditOpen(true);
                break;
            case POPUP_DELETE:
                setIsDeleteOpen(true);
                break;
            case POPUP_ADD:
                setIsAddOpen(true);
                break;
        }
    }

    function handlePopupClose() {
        setIsEditOpen(false);
        setIsDeleteOpen(false);
        setIsAddOpen(false);
        setIsPlusOpen(false);
        categoryStore.cancelAction();
    }

    return (
        <div style={{ margin: '80px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '20px' }}>
                <h3>Liste des catégories</h3>
                <Button style={{ marginLeft: '30px' }} onClick={() => handlePopupClick("add", null)}>Ajouter une categorie</Button>
            </div>
            <Row>
                {categoryStore.categories.map(categorie => (
                    <Col xs={12} md={6} lg={4} className="mb-3" key={categorie.id}>
                        <Card key={categorie.id}>
                            <Card.Header style={{ textAlign: 'center' }}>
                                <Card.Title>{categorie.name}</Card.Title>
                            </Card.Header>
                            <Card.Body>
                                <Card.Subtitle className="mb-2 text-muted">{categorie.adultOnly ? "Réservé +18" : "Accessible tout âge"}</Card.Subtitle>
                                <ListGroup variant="flush">
                                    <ListGroup.Item>Code : {categorie.code}</ListGroup.Item>
                                    <ListGroup.Item>Nom : {categorie.name}</ListGroup.Item>
                                </ListGroup>
                            </Card.Body>
                            <Card.Footer>
                                <Button variant="link" onClick={() => handlePopupClick("edit", categorie)}>Editer</Button>
                                <ConfirmationPopup title="Supprimer" message="Etes vous sur de vouloir supprimer cette catégorie ?" onAction={() => categoryStore.removeCategory(categorie.id)} />
                            </Card.Footer>
                        </Card>
                    </Col>
                ))}
            </Row>
            {isAddOpen &&
                (<AddCategoryPopup onSubmit={handleAddCategory} onCancel={handlePopupClose} />)
            }
            {isEditOpen &&
                (<CategorieEdit onClose={handlePopupClose} categorie={selectedCategory} />)
            }
        </div >
    );
}
export default observer(Categories);