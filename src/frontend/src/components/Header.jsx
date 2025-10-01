import * as React  from "react";
export default function Header(props) {
    return (
        <header className="App-header">
            <img src={props.logo} className="App-logo" alt="logo" />
            <h1>{props.pageTitle}</h1>
        </header>
    );
}