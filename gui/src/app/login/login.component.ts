import { Component, OnInit } from '@angular/core';
import { ConfigService } from "../core/services/config.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  constructor(private confService: ConfigService) { }

  ngOnInit() {
  }

  login() : void{
    this.confService.login();
  }

}
