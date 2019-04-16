package it.bz.idm.bdp.augeg4;

import it.bz.idm.bdp.augeg4.dto.tohub.AugeG4ToHubDataDto;
import it.bz.idm.bdp.augeg4.face.DataPusherFace;
import it.bz.idm.bdp.augeg4.mock.DataConverterMock;
import it.bz.idm.bdp.dto.DataTypeDto;
import it.bz.idm.bdp.dto.StationDto;
import it.bz.idm.bdp.dto.StationList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ContextConfiguration(locations = {"classpath:/META-INF/spring/applicationContext.xml"})
public class DataPusherTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private DataPusherFace dataPusher;

    @Test
    public void test_sync_stations() {
        // given
        StationList list = new StationList();
        StationDto station = new StationDto();
        station.setId("STATION A ID");
        station.setName("non-unique name for station");
        station.setStationType("STATION A");
        station.setOrigin("origin"); // The source of our data set
        list.add(station);

        // when
        dataPusher.syncStations(list);

        // then no exception is thrown
    }

    @Test
    public void test_sync_DataTypes() {
        // given
        DataTypeDto type = new DataTypeDto();
        type.setName("temperature");
        type.setPeriod(600);
        type.setUnit("°C");
        List<DataTypeDto> types = Collections.singletonList(type);

        // when
        dataPusher.syncDataTypes(types);

        // then no exception is thrown
    }

    @Test
    public void test_push_data() {
        // given
        List<AugeG4ToHubDataDto> mockedData = new DataConverterMock().convert(new ArrayList<>());

        // when
        dataPusher.mapData(mockedData);
        dataPusher.pushData();

        // then no exception is thrown
    }
}
