package cn.emac.demo.spring5.repositories;

import cn.emac.demo.spring5.domain.Employee;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EmployeeRepository extends PagingAndSortingRepository<Employee, Long> {

}