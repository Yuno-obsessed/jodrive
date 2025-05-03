import {Header} from "../widgets/Header.jsx";
import Sidebar from "../widgets/Sidebar.jsx";

export const MainLayout = () => {
    return (
        <div className="layout">
            <Header/>
            <Sidebar/>
        </div>
    )
}