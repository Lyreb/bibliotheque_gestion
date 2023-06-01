import React from 'react';
import Navbar from 'react-bootstrap/Navbar';
import Nav from 'react-bootstrap/Nav';
import { NavLink, Link } from 'react-router-dom';

function AppNavbar() {
  return (
    <Navbar expand="xxl" fixed="top" bg="dark" variant="dark" className="py-1">
      {/* <Navbar.Brand as={Link} to="/">
        {APP_ENV.APP_TITLE}
      </Navbar.Brand> */}
      <Navbar.Toggle aria-controls="AppNavbar" />
      <Navbar.Collapse id="AppNavbar">
        <Nav>
          <Nav.Link as={NavLink} to="/">Membres</Nav.Link>
          <Nav.Link as={NavLink} to="/livres">Livres</Nav.Link>
          <Nav.Link as={NavLink} to="/categories">Categories</Nav.Link>
          <Nav.Link as={NavLink} to="/recherche">Recherche</Nav.Link>
          <Nav.Link as={NavLink} to="/emprunts">Emprunt</Nav.Link>
          <Nav.Link as={NavLink} to="/statistiques">Statistiques</Nav.Link>
        </Nav>
      </Navbar.Collapse>
    </Navbar>
  );
}

export default AppNavbar;