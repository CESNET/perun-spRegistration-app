import {Component, OnInit} from '@angular/core';
import {UsersService} from "../core/services/users.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  constructor(private userService: UsersService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.userService.getUser().subscribe(user => {
      if (user != null) {
        this.router.navigate(['/auth']);
      } else {
        this.userService.setUser().subscribe(success => {
          if (success) {
            this.router.navigate(['/auth']);
          }
        })
      }
    });
  }

}
