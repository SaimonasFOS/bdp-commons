package it.bz.idm.bdp.augeg4.face;

import it.bz.idm.bdp.augeg4.dto.toauge.AugeG4ProcessedDataToAugeDto;

import java.util.List;

public interface DataPusherAugeFace {
    void pushData(List<AugeG4ProcessedDataToAugeDto> data);
}
