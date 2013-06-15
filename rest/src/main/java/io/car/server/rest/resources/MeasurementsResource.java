/*
 * Copyright (C) 2013  Christian Autermann, Jan Alexander Wirwahn,
 *                     Arne De Wall, Dustin Demuth, Saqib Rasheed
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.car.server.rest.resources;

import io.car.server.core.entities.Measurement;
import io.car.server.core.entities.Measurements;
import io.car.server.core.entities.Track;
import io.car.server.core.entities.User;
import io.car.server.core.exception.MeasurementNotFoundException;
import io.car.server.core.exception.ResourceAlreadyExistException;
import io.car.server.core.exception.TrackNotFoundException;
import io.car.server.core.exception.UserNotFoundException;
import io.car.server.core.exception.ValidationException;
import io.car.server.core.util.Pagination;
import io.car.server.rest.MediaTypes;
import io.car.server.rest.RESTConstants;
import io.car.server.rest.Schemas;
import io.car.server.rest.auth.Authenticated;
import io.car.server.rest.validation.Schema;

import java.util.ArrayList;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author Arne de Wall <a.dewall@52north.org>
 */
public class MeasurementsResource extends AbstractResource {
    public static final String MEASUREMENT = "{measurement}";
    private final Track track;
    private final User user;

    @Inject
    public MeasurementsResource(@Assisted @Nullable Track track,
                                @Assisted @Nullable User user) {
        this.track = track;
        this.user = user;
    }

    @GET
    @Schema(response = Schemas.MEASUREMENTS)
    @Produces(MediaTypes.MEASUREMENTS)
    public Measurements get(
            @QueryParam(RESTConstants.LIMIT) @DefaultValue("0") int limit,
            @QueryParam(RESTConstants.PAGE) @DefaultValue("0") int page,
            @QueryParam(RESTConstants.BBOX) @DefaultValue("") String bbox)
            throws UserNotFoundException, TrackNotFoundException {
        Pagination p = new Pagination(limit, page);
        if (!bbox.equals("")) {
            final String[] coords = bbox.split(",");
            Double[] d = new ArrayList<Double>() {
                private static final long serialVersionUID = -5786311066498093873L;
                {
                    for (String coord : coords)
                        add(Double.parseDouble(coord));
                }
            }.toArray(new Double[coords.length]);
            return getService().getMeasurementsByBbox(d[0], d[1], d[2], d[3], p);
        }
        if (track == null) {
            if (user == null) {
                return getService().getMeasurements(p);
            } else {
                return getService().getMeasurements(user, p);
            }
        } else {
            return getService().getMeasurementsByTrack(track, p);
        }
    }

    @POST
    @Authenticated
    @Schema(request = Schemas.MEASUREMENT_CREATE)
    @Consumes(MediaTypes.MEASUREMENT_CREATE)
    public Response create(Measurement measurement) throws
            ResourceAlreadyExistException, ValidationException,
            UserNotFoundException {
        Measurement m;
        if (track != null) {
            checkRights(getRights().canModify(track));
            measurement.setUser(getCurrentUser());
            m = getService().createMeasurement(track, measurement);
        } else {
            measurement.setUser(getCurrentUser());
            m = getService().createMeasurement(measurement);
        }
        return Response.created(
                getUriInfo()
                .getAbsolutePathBuilder()
                .path(m.getIdentifier()).build()).build();
    }

    @Path(MEASUREMENT)
    public MeasurementResource measurement(@PathParam("measurement") String id)
            throws MeasurementNotFoundException {
        if (user != null) {
            checkRights(getRights().canSeeMeasurementsOf(user));
        }
        if (track != null) {
            checkRights(getRights().canSeeMeasurementsOf(track));
        }

        Measurement m = getService().getMeasurement(id);
        checkRights(getRights().canSee(m));
        return getResourceFactory().createMeasurementResource(m, user, track);
    }
}