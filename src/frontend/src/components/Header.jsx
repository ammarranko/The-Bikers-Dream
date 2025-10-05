import * as React from "react";
import "./Header.css";
import logo from "./assets/logo.png";

export default function Header(props) {
  return (
    <header className="App-header">
      <img src={logo} className="App-logo" alt="logo" />
      <h1>{props.pageTitle}</h1>
    </header>
  );
}
