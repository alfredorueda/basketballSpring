(function() {
    'use strict';

    angular
        .module('basketballApp')
        .controller('FavouritePlayerDeleteController',FavouritePlayerDeleteController);

    FavouritePlayerDeleteController.$inject = ['$uibModalInstance', 'entity', 'FavouritePlayer'];

    function FavouritePlayerDeleteController($uibModalInstance, entity, FavouritePlayer) {
        var vm = this;

        vm.favouritePlayer = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            FavouritePlayer.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
