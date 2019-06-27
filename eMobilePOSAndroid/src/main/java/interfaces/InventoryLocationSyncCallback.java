package interfaces;

import com.android.emobilepos.models.InventoryItem;

import java.util.List;

public interface InventoryLocationSyncCallback {
    void inventoryLocationsSynched(List<InventoryItem> onHandItems);
}
