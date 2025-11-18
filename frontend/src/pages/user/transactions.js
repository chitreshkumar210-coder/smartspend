import { useEffect, useState, useCallback } from 'react';
import UserService from '../../services/userService';
import AuthService from '../../services/auth.service';
import Header from '../../components/utils/header';
import Loading from '../../components/utils/loading';
import Search from '../../components/utils/search';
import usePagination from '../../hooks/usePagination';
import PageInfo from '../../components/utils/pageInfo';
import TransactionList from '../../components/userTransactions/transactionList.js';
import { useLocation } from 'react-router-dom';
import Info from '../../components/utils/Info.js';
import Container from '../../components/utils/Container.js';
import toast, { Toaster } from 'react-hot-toast';
import { exportToPDF, exportToExcel } from '../../utils/exportTransactions';


function Transactions() {

    const [userTransactions, setUserTransactions] = useState([]);
    const [isFetching, setIsFetching] = useState(true);
    const [transactionType, setTransactionType] = useState('');
    const [isExporting, setIsExporting] = useState(false);
    const location = useLocation();

    const {
        pageSize, pageNumber, noOfPages, sortField, sortDirec, searchKey,
        onNextClick, onPrevClick, setNoOfPages, setNoOfRecords, setSearchKey, getPageInfo
    } = usePagination('date')

    const getTransactions = useCallback(async () => {
        await UserService.get_transactions(AuthService.getCurrentUser().email, pageNumber,
            pageSize, searchKey, sortField, sortDirec, transactionType).then(
                (response) => {
                    if (response.data.status === "SUCCESS") {
                        setUserTransactions(response.data.response.data)
                        setNoOfPages(response.data.response.totalNoOfPages)
                        setNoOfRecords(response.data.response.totalNoOfRecords)
                        return
                    }
                },
                (error) => {
                    toast.error("Failed to fetch all transactions: Try again later!")
                }
            )
        setIsFetching(false)
    }, [pageNumber, pageSize, searchKey, sortField, sortDirec, transactionType, setNoOfPages, setNoOfRecords])

    useEffect(() => {
        getTransactions()
    }, [pageNumber, searchKey, transactionType, sortDirec, sortField, getTransactions])

    useEffect(() => {
        location.state && toast.success(location.state.text)
        location.state = null
    }, [location])

    // Fetch all transactions for export (respects current filters)
    const fetchAllTransactions = async () => {
        try {
            setIsExporting(true);
            const allTransactions = {};
            let currentPage = 1;
            let hasMore = true;
            const pageSize = 100; // Large page size to minimize API calls

            while (hasMore) {
                const response = await UserService.get_transactions(
                    AuthService.getCurrentUser().email,
                    currentPage,
                    pageSize,
                    searchKey || '', // Use current search filter or empty
                    sortField || 'date', // Use current sort or default to date
                    sortDirec || 'desc', // Use current sort direction or default to desc
                    transactionType || '' // Use current transaction type filter or empty
                );

                if (response.data.status === "SUCCESS") {
                    const transactions = response.data.response.data;
                    if (transactions && Object.keys(transactions).length > 0) {
                        // Merge transactions grouped by date
                        Object.keys(transactions).forEach((date) => {
                            if (!allTransactions[date]) {
                                allTransactions[date] = [];
                            }
                            allTransactions[date].push(...transactions[date]);
                        });
                    }

                    const totalPages = response.data.response.totalNoOfPages;
                    if (currentPage >= totalPages || totalPages === 0) {
                        hasMore = false;
                    } else {
                        currentPage++;
                    }
                } else {
                    hasMore = false;
                }
            }

            return Object.keys(allTransactions).length > 0 ? allTransactions : null;
        } catch (error) {
            console.error('Error fetching all transactions:', error);
            toast.error('Failed to fetch transactions for export');
            return null;
        } finally {
            setIsExporting(false);
        }
    };

    const handleExportPDF = async () => {
        const allTransactions = await fetchAllTransactions();
        if (allTransactions && Object.keys(allTransactions).length > 0) {
            exportToPDF(allTransactions, 'transactions_history');
            toast.success('PDF exported successfully!');
        } else {
            toast.error('No transactions to export');
        }
    };

    const handleExportExcel = async () => {
        const allTransactions = await fetchAllTransactions();
        if (allTransactions && Object.keys(allTransactions).length > 0) {
            exportToExcel(allTransactions, 'transactions_history');
            toast.success('Excel file exported successfully!');
        } else {
            toast.error('No transactions to export');
        }
    };

    return (
        <Container activeNavId={1}>
            <Header title="Transactions History" />
            <Toaster/>

            {(userTransactions.length === 0 && isFetching) && <Loading />}
            {(!isFetching) &&
                <>
                    <div className='utils'>
                        <Filter
                            setTransactionType={(val) => setTransactionType(val)}
                        />
                        <div className='page'>
                            <div style={{ display: 'flex', gap: '10px', alignItems: 'center', marginRight: '15px' }}>
                                <button
                                    onClick={handleExportPDF}
                                    disabled={isExporting}
                                    style={{
                                        padding: '8px 16px',
                                        backgroundColor: '#dc3545',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '4px',
                                        cursor: isExporting ? 'not-allowed' : 'pointer',
                                        fontSize: '14px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '5px'
                                    }}
                                    title="Download as PDF"
                                >
                                    <i className="fas fa-file-pdf" aria-hidden="true"></i>
                                    {isExporting ? 'Exporting...' : 'PDF'}
                                </button>
                                <button
                                    onClick={handleExportExcel}
                                    disabled={isExporting}
                                    style={{
                                        padding: '8px 16px',
                                        backgroundColor: '#28a745',
                                        color: 'white',
                                        border: 'none',
                                        borderRadius: '4px',
                                        cursor: isExporting ? 'not-allowed' : 'pointer',
                                        fontSize: '14px',
                                        display: 'flex',
                                        alignItems: 'center',
                                        gap: '5px'
                                    }}
                                    title="Download as Excel"
                                >
                                    <i className="fas fa-file-excel" aria-hidden="true"></i>
                                    {isExporting ? 'Exporting...' : 'Excel'}
                                </button>
                            </div>
                            <Search
                                onChange={(val) => setSearchKey(val)}
                                placeholder="Search transactions"
                            />
                            <PageInfo
                                info={getPageInfo()}
                                onPrevClick={onPrevClick}
                                onNextClick={onNextClick}
                                pageNumber={pageNumber}
                                noOfPages={noOfPages}
                            />
                        </div>
                    </div>
                    {(userTransactions.length === 0) && <Info text={"No transactions found!"} />}
                    {(userTransactions.length !== 0) && <TransactionList list={userTransactions} />}
                </>
            }
        </Container>
    )
}

export default Transactions;


function Filter({ setTransactionType }) {
    return (
        <select onChange={(e) => setTransactionType(e.target.value)} style={{ margin: '0 15px 0 0' }}>
            <option value="">All</option>
            <option value="expense">Expense</option>
            <option value="income">Income</option>
        </select>
    )
}


