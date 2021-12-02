import {Component, OnInit, AfterViewInit, OnDestroy} from "@angular/core";
import {ActivatedRoute} from "@angular/router";
import {ActivityService} from "../../services/activity-service";
import {ScenarioExecution} from "../../model/scenario";
import {MessageFilter, ScenarioExecutionFilter} from "../../model/filter";
import {MatFormField} from "@angular/material/form-field";

@Component({
    moduleId: module.id,
    templateUrl: 'activity.html',
    styleUrls: ['activity.css', '../../../assets/css/filter-section.css'],
    selector: "app-root",
})
export class ActivityComponent implements OnInit, OnDestroy, AfterViewInit {
    scenarioExecutions: ScenarioExecution[];
    scenarioExecutionFilter: ScenarioExecutionFilter;
    errorMessage: string;

    inputValue: string = '';
    inputHeaders: string = '';
    inputIncludeFilterInRequest: boolean = false;

    successState: boolean = true;
    failedState: boolean = true;
    activeState: boolean = true;

    dateTimeFrom: any;
    dateTimeTo: any;

    pageSize = 25;
    page = 0;
    autoRefreshId: number;

    constructor(
        private activityService: ActivityService,
        private route: ActivatedRoute) {
    }

    ngOnInit() {
        this.scenarioExecutionFilter = this.initScenarioExecutionFilter();
        this.getActivities();
        let statusFilter = this.route.snapshot.params['status'];
        if(statusFilter) {
            if (statusFilter.toLowerCase().indexOf("success") > -1) {
                this.toggleFailed();
                this.toggleActive();
            } else if(statusFilter.toLowerCase().indexOf("failed") > -1) {
                this.toggleSuccess();
                this.toggleActive();
            } else if(statusFilter.toLowerCase().indexOf("active") > -1) {
                this.toggleSuccess();
                this.toggleFailed();
            }
        }

        this.autoRefreshId = window.setInterval(() => { if (this.page == 0 && this.pageSize < 250) {
            this.getActivities();
        } }, 2000);
    }

    ngOnDestroy(): void {
        window.clearInterval(this.autoRefreshId);
    }

    ngAfterViewInit(): void {
    }

    getActivities() {
        this.buildLocalScenarioExecutionFilter();
        this.activityService.getScenarioExecutions(this.scenarioExecutionFilter).subscribe(
            scenarioExecutions => this.scenarioExecutions = scenarioExecutions,
            error => this.errorMessage = <any>error
        );
    }

    buildLocalScenarioExecutionFilter() {
        this.scenarioExecutionFilter.fromDate = this.dateTimeFrom;
        this.scenarioExecutionFilter.toDate = this.dateTimeTo;
        this.scenarioExecutionFilter.pageSize = this.pageSize;
        this.scenarioExecutionFilter.pageNumber = this.page;
        this.scenarioExecutionFilter.scenarioName = this.inputValue;
        this.scenarioExecutionFilter.headers = this.inputHeaders;
        this.scenarioExecutionFilter.states = ["success:" + this.successState ,"failed:" + this.failedState,"active:" + this.activeState];
    }

    clearActivity() {
        this.activityService.clearScenarioExecutions().subscribe({
            next:success => this.getActivities(),
            error:error => this.errorMessage = <any>error
        });
    }

    prev() {
        if (this.page > 0) {
            this.page--;
            this.getActivities();
        }
    }

    next() {
        if (this.scenarioExecutions && this.scenarioExecutions.length) {
            this.page++;
            this.getActivities();
        }
    }

    toggleSuccess() {
        this.successState = !this.successState;
    }

    toggleFailed() {
        this.failedState = !this.failedState;
    }

    toggleActive() {
        this.activeState = !this.activeState;
    }

    initScenarioExecutionFilter(): ScenarioExecutionFilter {
        return new ScenarioExecutionFilter(null, null, 0, 25, '', '', [] );
    }
}
