package org.kalasim.examples.taxiinc.vehiclerouting.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;
import org.optaplanner.core.api.domain.variable.NextElementShadowVariable;
import org.optaplanner.core.api.domain.variable.PreviousElementShadowVariable;
import org.kalasim.examples.taxiinc.vehiclerouting.domain.location.Location;
import org.kalasim.examples.taxiinc.vehiclerouting.domain.solver.DepotAngleCustomerDifficultyWeightFactory;

@PlanningEntity(difficultyWeightFactoryClass = DepotAngleCustomerDifficultyWeightFactory.class)
public class Customer extends AbstractPersistable {

    protected Location location;
    protected int demand;

    // Shadow variables
    protected Vehicle vehicle;
    protected Customer previousCustomer;
    protected Customer nextCustomer;

    public Customer() {
    }

    public Customer(long id, Location location, int demand) {
        super(id);
        this.location = location;
        this.demand = demand;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    @InverseRelationShadowVariable(sourceVariableName = "customers")
    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @PreviousElementShadowVariable(sourceVariableName = "customers")
    public Customer getPreviousCustomer() {
        return previousCustomer;
    }

    public void setPreviousCustomer(Customer previousCustomer) {
        this.previousCustomer = previousCustomer;
    }

    @NextElementShadowVariable(sourceVariableName = "customers")
    public Customer getNextCustomer() {
        return nextCustomer;
    }

    public void setNextCustomer(Customer nextCustomer) {
        this.nextCustomer = nextCustomer;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public long getDistanceFromPreviousStandstill() {
        if (vehicle == null) {
            throw new IllegalStateException(
                    "This method must not be called when the shadow variables are not initialized yet.");
        }
        if (previousCustomer == null) {
            return vehicle.getLocation().getDistanceTo(location);
        }
        return previousCustomer.getLocation().getDistanceTo(location);
    }

    public long getDistanceToDepot() {
        return location.getDistanceTo(vehicle.getLocation());
    }

    @Override
    public String toString() {
        if (location.getName() == null) {
            return super.toString();
        }
        return location.getName();
    }

}