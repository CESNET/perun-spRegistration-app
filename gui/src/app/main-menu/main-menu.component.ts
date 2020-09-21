import {Component, OnInit} from '@angular/core';
import {faClipboardList, faNotesMedical, faServer, faToolbox} from '@fortawesome/free-solid-svg-icons';
import {User} from '../core/models/User';
import {AppComponent} from '../app.component';
import {UsersService} from "../core/services/users.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-main-menu',
  templateUrl: './main-menu.component.html',
  styleUrls: ['./main-menu.component.scss']
})
export class MainMenuComponent implements OnInit {

  constructor(
    private router: Router,
    private usersService: UsersService,
  ) { }

  requestsIcon = faClipboardList;
  facilitiesIcon = faServer;
  newRequestIcon = faNotesMedical;
  toolboxIcon = faToolbox;

  ngOnInit() {
    this.usersService.getUser().subscribe(user => {
      if (user) {
        AppComponent.setUser(new User(user))
      } else {
        this.router.navigate(['/']);
        AppComponent.setUser(null);
      }
    });
  }

  getUser(): User {
    return AppComponent.getUser();
  }

  hasUser(): boolean {
    return AppComponent.hasUser();
  }

  isUserAppAdmin(): boolean {
    return this.getUser().isAppAdmin;
  }
}
