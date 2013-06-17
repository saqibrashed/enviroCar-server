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
package io.car.server.core;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import io.car.server.core.dao.MeasurementDao;
import io.car.server.core.dao.PhenomenonDao;
import io.car.server.core.dao.SensorDao;
import io.car.server.core.dao.TrackDao;
import io.car.server.core.entities.Measurement;
import io.car.server.core.entities.Measurements;
import io.car.server.core.entities.Phenomenon;
import io.car.server.core.entities.Phenomenons;
import io.car.server.core.entities.Sensor;
import io.car.server.core.entities.Sensors;
import io.car.server.core.entities.Track;
import io.car.server.core.entities.Tracks;
import io.car.server.core.event.ChangedMeasurementEvent;
import io.car.server.core.event.ChangedTrackEvent;
import io.car.server.core.event.CreatedMeasurementEvent;
import io.car.server.core.event.CreatedPhenomenonEvent;
import io.car.server.core.event.CreatedSensorEvent;
import io.car.server.core.event.CreatedTrackEvent;
import io.car.server.core.event.DeletedMeasurementEvent;
import io.car.server.core.event.DeletedTrackEvent;
import io.car.server.core.exception.IllegalModificationException;
import io.car.server.core.exception.MeasurementNotFoundException;
import io.car.server.core.exception.PhenomenonNotFoundException;
import io.car.server.core.exception.SensorNotFoundException;
import io.car.server.core.exception.TrackNotFoundException;
import io.car.server.core.exception.ValidationException;
import io.car.server.core.filter.MeasurementFilter;
import io.car.server.core.filter.SensorFilter;
import io.car.server.core.filter.TrackFilter;
import io.car.server.core.update.EntityUpdater;
import io.car.server.core.util.Pagination;
import io.car.server.core.validation.EntityValidator;

/**
 * @author Christian Autermann <autermann@uni-muenster.de>
 * @author Arne de Wall <a.dewall@52north.org>
 * @author Jan Wirwahn <jan.wirwahn@wwu.de>
 */
public class DataServiceImpl implements DataService {
    private final TrackDao trackDao;
    private final MeasurementDao measurementDao;
    private final SensorDao sensorDao;
    private final PhenomenonDao phenomenonDao;
    private final EntityValidator<Track> trackValidator;
    private final EntityUpdater<Track> trackUpdater;
    private final EntityUpdater<Measurement> measurementUpdater;
    private final EntityValidator<Measurement> measurementValidator;
    private final EventBus eventBus;

    @Inject
    public DataServiceImpl(TrackDao trackDao, MeasurementDao measurementDao,
                           SensorDao sensorDao, PhenomenonDao phenomenonDao,
                           EntityValidator<Track> trackValidator,
                           EntityUpdater<Track> trackUpdater,
                           EntityUpdater<Measurement> measurementUpdater,
                           EntityValidator<Measurement> measurementValidator,
                           EventBus eventBus) {
        this.trackDao = trackDao;
        this.measurementDao = measurementDao;
        this.sensorDao = sensorDao;
        this.phenomenonDao = phenomenonDao;
        this.trackValidator = trackValidator;
        this.trackUpdater = trackUpdater;
        this.measurementUpdater = measurementUpdater;
        this.measurementValidator = measurementValidator;
        this.eventBus = eventBus;
    }

    @Override
    public Track modifyTrack(Track track, Track changes)
            throws ValidationException, IllegalModificationException {
        this.trackValidator.validateCreate(track);
        this.trackUpdater.update(changes, track);
        this.trackDao.save(track);
        this.eventBus.post(new ChangedTrackEvent(track.getUser(), track));
        return track;
    }

    @Override
    public Track getTrack(String id) throws TrackNotFoundException {
        Track track = trackDao.getById(id);
        if (track == null) {
            throw new TrackNotFoundException(id);
        }
        return track;
    }

    @Override
    public Track createTrack(Track track) throws ValidationException {
        this.trackValidator.validateCreate(track);
        this.trackDao.create(track);
        this.eventBus.post(new CreatedTrackEvent(track.getUser(), track));
        return track;
    }

    @Override
    public void deleteTrack(Track track) {
        this.trackDao.delete(track);
        this.eventBus.post(new DeletedTrackEvent(track, track.getUser()));
    }

    @Override
    public Measurement createMeasurement(Measurement m) {
        this.measurementValidator.validateCreate(m);
        this.measurementDao.create(m);
        this.eventBus.post(new CreatedMeasurementEvent(m.getUser(), m));
        return m;
    }

    @Override
    public Measurement createMeasurement(Track track, Measurement m) {
        this.measurementValidator.validateCreate(m);
        m.setTrack(track);
        this.measurementDao.create(m);
        this.trackDao.update(track);
        this.eventBus.post(new CreatedMeasurementEvent(m.getUser(), m));
        return m;
    }

    @Override
    public Measurement getMeasurement(String id) throws
            MeasurementNotFoundException {
        Measurement m = this.measurementDao.getById(id);
        if (m == null) {
            throw new MeasurementNotFoundException(id);
        }
        return m;
    }

    @Override
    public Measurement modifyMeasurement(Measurement m,
                                         Measurement changes)
            throws ValidationException, IllegalModificationException {
        this.measurementValidator.validateCreate(m);
        this.measurementUpdater.update(changes, m);
        this.measurementDao.save(m);
        this.eventBus.post(new ChangedMeasurementEvent(m, m.getUser()));
        return m;
    }

    @Override
    public void deleteMeasurement(Measurement m) {
        this.measurementDao.delete(m);
        this.eventBus.post(new DeletedMeasurementEvent(m, m.getUser()));
    }

    @Override
    public Phenomenon getPhenomenonByName(String name)
            throws PhenomenonNotFoundException {
        Phenomenon p = this.phenomenonDao.getByName(name);
        if (p == null) {
            throw new PhenomenonNotFoundException(name);
        }
        return p;
    }

    @Override
    public Phenomenon createPhenomenon(Phenomenon phenomenon) {
        this.phenomenonDao.create(phenomenon);
        this.eventBus.post(new CreatedPhenomenonEvent(phenomenon));
        return phenomenon;
    }

    @Override
    public Phenomenons getPhenomenons(Pagination p) {
        return this.phenomenonDao.get(p);
    }

    @Override
    public Sensor getSensorByName(String id) throws SensorNotFoundException {
        Sensor s = this.sensorDao.getByIdentifier(id);
        if (s == null) {
            throw new SensorNotFoundException(id);
        }
        return s;
    }

    @Override
    public Sensor createSensor(Sensor sensor) {
        this.sensorDao.create(sensor);
        this.eventBus.post(new CreatedSensorEvent(sensor));
        return sensor;
    }

    @Override
    public Measurements getMeasurements(MeasurementFilter request) {
        return this.measurementDao.get(request);
    }

    @Override
    public Tracks getTracks(TrackFilter request) {
        return this.trackDao.get(request);
    }

    @Override
    public Sensors getSensors(SensorFilter request) {
        return this.sensorDao.get(request);
    }
}