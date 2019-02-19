import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {RequestsOverviewComponent} from "./requests-overview/requests-overview.component";
import {NewRequestComponent} from "./new-request/new-request.component";
import {RequestDetailComponent} from "./request-detail/request-detail.component";
import {AllRequestsComponent} from "./all-requests/all-requests.component";

const routes: Routes = [
  {
    path: 'myRequests',
    component: RequestsOverviewComponent
  },
  {
    path: 'allRequests',
    component: AllRequestsComponent
  },
  {
    path: 'new',
    component: NewRequestComponent
  },
  {
    path: 'detail/:id',
    component: RequestDetailComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RequestsRoutingModule { }