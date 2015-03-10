var app = angular.module('jobs', ['ngRoute']);

app.factory('$jobs', function ($http) {
   return {
       create: function (job) {
           return $http.post('/api/job', job);
       },
       list: function() {
           return $http.get('/api/job');
       },
       delete: function(id) {
           return $http.delete('/api/job/' + id);
       }
   };
});

app.controller('Jobs', function($scope, $routeParams, $location, $jobs) {

    $scope.jobs = {};

    $jobs.list().success(function (data) {
        $scope.jobs = data;
    });

    if ($routeParams.id) {
        $scope.jobid = $routeParams.id;
    }

    $scope.job_list = function() {
        var l = [];
        for (var k in $scope.jobs) {
            $scope.jobs[k].id = k;
            l.push($scope.jobs[k]);
        }
        return l;
    };

    $scope.create_job = function (job) {
        $jobs.create(job).success(function (data) {
            $scope.jobs = data;
            $location.path('/jobs');
        });
    };

    $scope.delete_job = function (jobid) {
        $jobs.delete(jobid).success(function (data) {
            $scope.jobs = data;
            $location.path('/jobs');
        });
    };
});

app.config(function($routeProvider) {
    $routeProvider
        .when('/list', {templateUrl: 'listing.html', controller: 'Jobs'})
        .when('/details/:id', {templateUrl: 'details.html', controller: 'Jobs'})
        .when('/post', {templateUrl: 'post.html', controller: 'Jobs'})
        .otherwise({redirectTo: '/list'});
});
