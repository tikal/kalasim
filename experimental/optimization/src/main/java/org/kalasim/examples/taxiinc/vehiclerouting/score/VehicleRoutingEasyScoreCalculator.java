package org.kalasim.examples.taxiinc.vehiclerouting.score;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.calculator.EasyScoreCalculator;
import org.kalasim.examples.taxiinc.vehiclerouting.domain.Customer;
import org.kalasim.examples.taxiinc.vehiclerouting.domain.Vehicle;
import org.kalasim.examples.taxiinc.vehiclerouting.domain.VehicleRoutingSolution;
import org.kalasim.examples.taxiinc.vehiclerouting.domain.timewindowed.TimeWindowedCustomer;
import org.kalasim.examples.taxiinc.vehiclerouting.domain.timewindowed.TimeWindowedVehicleRoutingSolution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleRoutingEasyScoreCalculator
        implements EasyScoreCalculator<VehicleRoutingSolution, HardSoftLongScore> {

    @Override
    public HardSoftLongScore calculateScore(VehicleRoutingSolution solution) {
        boolean timeWindowed = solution instanceof TimeWindowedVehicleRoutingSolution;
        List<Customer> customerList = solution.getCustomerList();
        List<Vehicle> vehicleList = solution.getVehicleList();
        Map<Vehicle, Integer> vehicleDemandMap = new HashMap<>(vehicleList.size());
        for (Vehicle vehicle : vehicleList) {
            vehicleDemandMap.put(vehicle, 0);
        }
        long hardScore = 0L;
        long softScore = 0L;
        for (Customer customer : customerList) {
            Vehicle vehicle = customer.getVehicle();
            if (vehicle != null) {
                vehicleDemandMap.put(vehicle, vehicleDemandMap.get(vehicle) + customer.getDemand());
                // Score constraint distanceToPreviousStandstill
                softScore -= customer.getDistanceFromPreviousStandstill();
                if (customer.getNextCustomer() == null) {
                    // Score constraint distanceFromLastCustomerToDepot
                    softScore -= customer.getLocation().getDistanceTo(vehicle.getLocation());
                }
                if (timeWindowed) {
                    TimeWindowedCustomer timeWindowedCustomer = (TimeWindowedCustomer) customer;
                    long dueTime = timeWindowedCustomer.getDueTime();
                    Long arrivalTime = timeWindowedCustomer.getArrivalTime();
                    if (dueTime < arrivalTime) {
                        // Score constraint arrivalAfterDueTime
                        hardScore -= (arrivalTime - dueTime);
                    }
                }
            }
        }
        for (Map.Entry<Vehicle, Integer> entry : vehicleDemandMap.entrySet()) {
            int capacity = entry.getKey().getCapacity();
            int demand = entry.getValue();
            if (demand > capacity) {
                // Score constraint vehicleCapacity
                hardScore -= (demand - capacity);
            }
        }
        // Score constraint arrivalAfterDueTimeAtDepot is a built-in hard constraint in VehicleRoutingImporter
        return HardSoftLongScore.of(hardScore, softScore);
    }

}
