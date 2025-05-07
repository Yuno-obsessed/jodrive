import React, {useEffect} from 'react';
import './App.css'
import {MainLayout} from "./layouts/MainLayout.jsx";
import {FileSearchPage} from "./pages/FileSearchPage.jsx";
import useAuthStore from "./util/authStore.js";

function App() {
    const {authenticated, initKeycloak} = useAuthStore();

    useEffect(() => {
        initKeycloak();
    }, []);

    return (
        <>
            {authenticated ? (
                <MainLayout>
                    <FileSearchPage/>
                </MainLayout>
            ) : (
                <div>Loading...</div>
            )}
        </>
    );
}

export default App;