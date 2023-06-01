import { CategoryStore } from "./model/CategoryStore";
import { MemberStore } from "./model/MemberStore";
import { BooksStore } from "./model/BooksStore";
import { BookSearchStore } from "./model/SearchStore"

const STORE = {
    categoryStore: new CategoryStore(),
    memberStore: new MemberStore(),
    bookStore: new BooksStore(),
    bookSearchStore: new BookSearchStore(),
};

export default STORE;
