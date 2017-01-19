(function() {
    'use strict';

    angular
        .module('basketballApp')
        .controller('GameRatingDetailController', GameRatingDetailController);

    GameRatingDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'GameRating', 'User', 'Game'];

    function GameRatingDetailController($scope, $rootScope, $stateParams, previousState, entity, GameRating, User, Game) {
        var vm = this;

        vm.gameRating = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('basketballApp:gameRatingUpdate', function(event, result) {
            vm.gameRating = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
