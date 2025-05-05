import React from 'react';
import './App.css'
import {MainLayout} from "./layouts/MainLayout.jsx";
import {FileSearchPage} from "./pages/FileSearchPage.jsx";

function App() {
    return (
        <>
            <MainLayout>
                <FileSearchPage/>
            </MainLayout>
        </>
    );
}

export default App;