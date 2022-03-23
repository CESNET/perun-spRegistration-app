import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {RequestsUserComponent} from './requests-user/requests-user.component';
import {RequestsRegisterServiceComponent} from './requests-register-service/requests-register-service.component';
import {RequestsDetailComponent} from './requests-detail/requests-detail.component';
import {RequestsAdminComponent} from './requests-admin/requests-admin.component';
import {RequestsEditComponent} from './requests-edit/requests-edit.component';
import {NotFoundPageComponent} from '../shared/not-found-page/not-found-page.component';

const routes: Routes = [
  {
    path: 'myRequests',
    component: RequestsUserComponent
  },
  {
    path: 'allRequests',
    component: RequestsAdminComponent
  },
  {
    path: 'new',
    component: RequestsRegisterServiceComponent
  },
  {
    path: 'detail/:id',
    component: RequestsDetailComponent
  },
  {
    path: 'editRequest/:id',
    component: RequestsEditComponent
  },
  {
    path: '**',
    component: NotFoundPageComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RequestsRoutingModule { }
