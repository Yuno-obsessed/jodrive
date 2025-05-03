import {SearchBar} from "../components/SearchBar.jsx";
import './Header.css'
import Profile from "../components/Profile.jsx";

export const Header = () => {
    return (
        <header className="header-main">
            <div className="logo">
                <img src="jodrive-logo.png" alt="logo"/>
            </div>
            <SearchBar/>
            <Profile/>
        </header>
    );
}