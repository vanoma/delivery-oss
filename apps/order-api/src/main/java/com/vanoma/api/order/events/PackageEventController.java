package com.vanoma.api.order.events;

import com.vanoma.api.order.utils.annotations.PutMappingJson;
import com.vanoma.api.order.utils.annotations.RequestMappingJson;
import com.vanoma.api.order.utils.annotations.PostMappingJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMappingJson
public class PackageEventController {
    @Autowired
    private IPackageEventService packageEventService;

    @PutMappingJson(value = "/packages/{packageId}/events")
    public ResponseEntity<PackageEvent> createPackageEvent(@PathVariable String packageId,
                                                           @RequestBody PackageEventJson packageEventJson) {
        return ResponseEntity.ok(this.packageEventService.createPackageEvent(packageId, packageEventJson));
    }
}
