export class MessageFilter {
    constructor(public fromDate: number,
                public toDate: number,
                public pageNumber: number,
                public pageSize: number,
                public directionInbound: boolean,
                public directionOutbound: boolean,
                public containingText: string) {
    }
}

export class ScenarioExecutionFilter {
    constructor(
        public fromDate: number,
        public toDate: number,
        public pageNumber: number,
        public pageSize: number,
        public scenarioName: string,
        public headers: string,
        public states: string[],
    ) {
    }
}

export class ScenarioFilter {
    constructor(public name: string) {
    }
}
