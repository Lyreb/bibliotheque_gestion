import { makeAutoObservable, runInAction } from "mobx";

class MemberStore {
  members = [];
  isLoading = false;
  confirmationModal = null;
  confirmationAction = null;
  confirmationMessage = "";
  error = null;

  constructor() {
    makeAutoObservable(this)
  }

  // CHARGEMENT
  async fetchMembers() {
    this.isLoading = true;
    this.error = null;
    try {
      const response = await fetch("http://127.0.0.1:8080/api/v1/rest/members");
      const data = await response.json();
      runInAction(() => {
        this.members = data;
        this.isLoading = false;
      })

    } catch (error) {
      console.log("Erreur de récupération des membres", error);
      this.isLoading = false;
      this.error = error;
    }
  }

  // AJOUT
  async addMember(member) {
    try {
      const response = await fetch("http://127.0.0.1:8080/api/v1/rest/members", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(member),
      });
      if (response.ok) {
        const newMember = await response.json();
        this.members.push(newMember);
        this.confirmationMessage = "Member added successfully";
      } else {
        this.confirmationMessage = "Error adding member";
      }
    } catch (error) {
      console.log("Error adding member:", error);
      this.confirmationMessage = "Error adding member";
    }

  }
  async createMember(member) {
    await this.addMember(member);
  }

  // EDIT
  async editMember(id, updatedMember = { name: '', firstname: '', birthday: '' }) {
    console.log('edit')
    fetch(`http://127.0.0.1:8080/api/v1/rest/members/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ ...updatedMember, id }),
    })
      .then((res) => res.json())
      .then(
        (updatedMember) => {
          const idx = this.members.findIndex((m) => m.id === id);
          if (idx === -1) {
            console.warn('ca devrait pas');
            return;
          }
          runInAction(() => {
            this.members.splice(idx, 1, updatedMember);
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
  async removeMember(id) {
    try {
      const response = await fetch(`http://127.0.0.1:8080/api/v1/rest/members/${id}`, {
        method: "DELETE",
      });

      if (response.ok) {
        this.members = this.members.filter((member) => member.id !== id);
        this.confirmationMessage = "members removed successfully";
      } else {
        this.confirmationMessage = "Error removing members";
      }
    } catch (error) {
      console.log("Error removing members:", error);
      this.confirmationMessage = "Error removing members";
    }
  }

  // RETOURNER UN EXEMPLAIRE DE LIVRE
  async returnBook(bookCopyId, memberId, bookId) {
    try {
      await Promise.all([
        fetch(`http://127.0.0.1:8080/api/v1/rest/members/${memberId}`, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            members: this.members.find(m => m.id === memberId).loans.map(l =>
              l.bookCopy.id === bookCopyId
                ? { ...l, returnDateTime: new Date().toISOString() }
                : l
            )
          })
        }),
        fetch(`http://127.0.0.1:8080/api/v1/rest/books/${bookId}`, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            removed: false
          })
        })
      ]);
    } catch (error) {
      console.error("Erreur lors du retour du livre", error);
      throw error;
    }
  }
}
export { MemberStore };