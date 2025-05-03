import React from "react";
import './Sidebar.css'

export const Sidebar = () => {
    return (
        <aside className="sidebar">
            {/*<aside className="w-50 bg-white border-r p-4">*/}
            {/*<div className="mb-5">*/}
            <button className="btn-new">
                <span>
                    <img src="plus.svg" alt="Plus-New" className="btn-new-img"/>
                </span>
                <span className="btn-new-txt">
                    <a>New</a>
                </span>
            </button>
            <ul className="sidebar-list">
                <li className="sidebar-el">
                    <img src="home.svg" alt="Home" className="sidebar-images"/>
                    <a href="/" className="sidebar-links">Home</a>
                </li>
                <li className="sidebar-el">
                    <img src="drive.svg" alt="Drive" className="sidebar-images"/>
                    <a href="/my-drive" className="sidebar-links">My Drive</a>
                </li>
                <li className="sidebar-el">
                    <img src="workspaces.svg" alt="Workspaces" className="sidebar-images"/>
                    <a href="/workspaces" className="sidebar-links">Workspaces</a>
                </li>
                <li className="sidebar-el">
                    <img src="cloud.svg" alt="Storage" className="sidebar-images"/>
                    <a href="/storage" className="sidebar-links">Storage</a>
                </li>
                <li className="sidebar-el">
                    <div className="storage-bar">
                        <div className="storage-bar-progress"/>
                        <a className="storage-info">X GB of y GB used</a>
                    </div>
                </li>
            </ul>
        </aside>
    )
}

export default Sidebar