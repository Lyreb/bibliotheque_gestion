import {makeAutoObservable, runInAction } from "mobx";

class CategoryStore {
    categories = [];
    isLoading = false;
    confirmationModal = null;
    confirmationAction = null;
    confirmationMessage = "";
    error = null;

    constructor() {
        makeAutoObservable(this)
    }
    
    // CHARGEMENT DES CATEGORIES
    async fetchCategories(){
        this.isLoading = true;
        this.error = null;
        try {
            const response = await fetch("http://127.0.0.1:8080/api/v1/rest/categories");
            const data = await response.json();
            runInAction(()=> {
                this.categories = data;
                this.isLoading = false;
            })
            
        } catch (error) {
            console.log("Erreur de récupération des catégories", error);
            this.isLoading = false;
            this.error = error;
        }
    }

    // CREATION
    async addCategory(category) {
        try {
                const response = await fetch("http://127.0.0.1:8080/api/v1/rest/categories", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(category),
            });

            if (response.ok) {
                const newCategory = await response.json();
                this.categories.push(newCategory);
                this.confirmationMessage = "Category added successfully";
            } else {
                this.confirmationMessage = "Error adding category";
            }
        } catch (error) {
            console.log("Error adding category:", error);
            this.confirmationMessage = "Error adding category";
        }
    }

    async createCategory(category) {
        await this.addCategory(category);
    }

    // EDIT
    async editCategory(id, updateCategory = { name: '', code: '', adultOnly: '' }) {
        console.log('edit')
        fetch(`http://127.0.0.1:8080/api/v1/rest/categories/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({...updateCategory, id}),
        })
        .then((res) => res.json())
        .then(
            (updateCategory) => {
            const idx = this.categories.findIndex((c) => c.id === id);
            if (idx === -1){
                console.warn('ça devrait pas');
                return;
            }
            runInAction(() => {
                this.categories.splice(idx, 1, updateCategory);
            });
            }
        ).catch(
            (error) => {
            runInAction(() => {
                this.error = error;
            });
            throw error;
        });
    }
    
    // SUPPRESSION
    async removeCategory(id) {
        try {
            const response = await fetch(`http://127.0.0.1:8080/api/v1/rest/categories/${id}`, {
            method: "DELETE",
            });

            if (response.ok) {
            this.categories = this.categories.filter((category) => category.id !== id);
            this.confirmationMessage = "Category removed successfully";
            } else {
            this.confirmationMessage = "Error removing category";
            }
        } catch (error) {
            console.log("Error removing category:", error);
            this.confirmationMessage = "Error removing category";
        }
    }

    get getCategoryById() {
        return (id) => this.categories.find((category) => category.id === id);
    }

    confirmAction(action, message) {
        this.confirmationModal = true;
        this.confirmationAction = action;
        this.confirmationMessage = message;
    }

    cancelAction() {
        this.confirmationModal = false;
        this.confirmationAction = null;
        this.confirmationMessage = "";
    }
}

export { CategoryStore };